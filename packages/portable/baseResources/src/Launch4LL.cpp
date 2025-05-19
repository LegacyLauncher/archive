// launcher.cpp – 32‑bit stub that:
//   • picks a compatible JVM (prefers native arch, falls back to others)
//   • is DPI‑aware
//   • supports argument files:
//       - launcher.args read automatically when no CLI args are given
//
// Build (Win32):  cl /EHsc /std:c++17 /O2 launcher.cpp /link user32.lib shell32.lib

#define _SILENCE_CXX17_CODECVT_HEADER_DEPRECATION_WARNING
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <shellapi.h>
#include <wow64apiset.h>
#include <filesystem>
#include <fstream>
#include <string>
#include <vector>
#include <thread>
#include <locale>
#include <codecvt>
#include <sstream>

// ------------------------------------------------------------
// DPI helpers (avoid <shcore.h>)
// ------------------------------------------------------------
enum PROCESS_DPI_AWARENESS {
    PROCESS_DPI_UNAWARE = 0,
    PROCESS_SYSTEM_DPI_AWARE = 1,
    PROCESS_PER_MONITOR_DPI_AWARE = 2
};

static void enable_dpi_awareness()
{
    if (HMODULE u32 = GetModuleHandleW(L"user32.dll")) {
        using Fn = BOOL(WINAPI*)(HANDLE);
        if (auto p = reinterpret_cast<Fn>(GetProcAddress(u32, "SetProcessDpiAwarenessContext"))) {
            const HANDLE PER_MONITOR_V2 = reinterpret_cast<HANDLE>(-4);
            if (p(PER_MONITOR_V2)) return;
        }
    }
    if (HMODULE shc = LoadLibraryW(L"shcore.dll")) {
        using Fn = HRESULT(WINAPI*)(PROCESS_DPI_AWARENESS);
        if (auto p = reinterpret_cast<Fn>(GetProcAddress(shc, "SetProcessDpiAwareness"))) {
            p(PROCESS_PER_MONITOR_DPI_AWARE);
        }
    }
    SetProcessDPIAware();
}

// ------------------------------------------------------------
// CPU detection (callable from 32‑bit)
// ------------------------------------------------------------
enum class HostArch { X86, X64, ARM64, Unknown };

static HostArch detect_host() noexcept
{
    // ------------------------------------------------------------
    // 1. Try IsWow64Process2   (only present on Win10 1511+)
    // ------------------------------------------------------------
    if (HMODULE hKrnl = ::GetModuleHandleW(L"kernel32.dll"))
    {
        using FnIsWow64Process2 = BOOL(WINAPI*)(HANDLE, USHORT*, USHORT*);
        if (auto pIsWow64Process2 =
            reinterpret_cast<FnIsWow64Process2>(
                ::GetProcAddress(hKrnl, "IsWow64Process2")))
        {
            USHORT processMachine = 0, nativeMachine = 0;
            if (pIsWow64Process2(::GetCurrentProcess(),
                &processMachine, &nativeMachine))
            {
                switch (nativeMachine)
                {
                case IMAGE_FILE_MACHINE_AMD64: return HostArch::X64;
                case IMAGE_FILE_MACHINE_ARM64: return HostArch::ARM64; // Win 10/11 on ARM
                case IMAGE_FILE_MACHINE_I386: return HostArch::X86;
                default: return HostArch::Unknown;
                }
            }
        }
    }

    // ------------------------------------------------------------
    // 2. Fallback for Win7/8/8.1/older 10 : GetNativeSystemInfo
    //    (present on every OS we still care about)
    // ------------------------------------------------------------
    SYSTEM_INFO si{};
    ::GetNativeSystemInfo(&si);

    switch (si.wProcessorArchitecture)
    {
    case PROCESSOR_ARCHITECTURE_AMD64: return HostArch::X64;
    case PROCESSOR_ARCHITECTURE_ARM64: return HostArch::ARM64; // never reached pre-Win10
    case PROCESSOR_ARCHITECTURE_INTEL: return HostArch::X86;
    default: return HostArch::Unknown;
    }
}

// ------------------------------------------------------------
// Architecture‑stack builder
// ------------------------------------------------------------
static std::vector<std::wstring> build_arch_stack(HostArch host)
{
    switch (host) {
    case HostArch::ARM64: return { L"arm64", L"x64", L"x86" };
    case HostArch::X64:   return { L"x64",   L"x86" };
    case HostArch::X86:   return { L"x86" };
    default:              return { L"x86" };   // safest, most compatible
    }
}

// ------------------------------------------------------------
// Filesystem and quoting helpers
// ------------------------------------------------------------
static std::filesystem::path exe_dir()
{
    wchar_t buf[MAX_PATH];
    DWORD n = GetModuleFileNameW(nullptr, buf, MAX_PATH);
    return std::filesystem::path(buf, buf + n).remove_filename();
}

static std::wstring win_quote(const std::wstring& in)
{
    if (in.find_first_of(L" \t\"") == std::wstring::npos)
        return in;
    std::wstring out; out.reserve(in.size() + 2); out.push_back(L'"');
    unsigned bs = 0;
    for (wchar_t ch : in) {
        if (ch == L'\\') { ++bs; }
        else if (ch == L'"') {
            out.append(bs * 2 + 1, L'\\');
            out.push_back(ch);
            bs = 0;
        }
        else {
            out.append(bs, L'\\');
            bs = 0;
            out.push_back(ch);
        }
    }
    out.append(bs, L'\\');
    out.push_back(L'"');
    return out;
}

// ------------------------------------------------------------
// Arg‑file utilities
// ------------------------------------------------------------
static std::wstring unescape(const std::wstring& in)
{
    std::wstring out; out.reserve(in.size());
    for (size_t i = 0; i < in.size(); ++i) {
        if (in[i] == L'\\' && i + 1 < in.size()) {
            wchar_t nxt = in[i + 1];
            if (nxt == L'n') { out.push_back(L'\\n'); ++i; continue; }
            if (nxt == L'\\') { out.push_back(L'\\'); ++i; continue; }
        }
        out.push_back(in[i]);
    }
    return out;
}

static void load_argfile(const std::filesystem::path& file,
    std::vector<std::wstring>& out)
{
    std::wifstream fin(file);
    if (!fin.is_open()) return;          // ignore missing file
    fin.imbue(std::locale(fin.getloc(),
        new std::codecvt_utf8_utf16<wchar_t>));
    std::wstring line;
    while (std::getline(fin, line)) {
        if (!line.empty() && line.back() == L'\\r') line.pop_back();
        if (line.empty()) continue;
        out.push_back(unescape(line));
    }
}

// ------------------------------------------------------------
// Launch JVM
// ------------------------------------------------------------
static int launch_java()
{
    const HostArch host = detect_host();
    const auto arch_stack = build_arch_stack(host);

    const auto base = exe_dir();

    std::filesystem::path java_exe;

    for (const auto& sub : arch_stack) {
        auto candidate = base / L"jre" / sub / L"bin" / L"java.exe";
        if (std::filesystem::exists(candidate)) {
            java_exe = std::move(candidate);
            break;
        }
    }

    if (java_exe.empty()) {
        std::wstringstream ss;
        ss << L"Unable to locate a compatible JRE.\nSearched paths:\n";
        for (const auto& sub : arch_stack) {
            ss << L"  " << (base / L"jre" / sub / L"bin" / L"java.exe").wstring() << L"\n";
        }
        MessageBoxW(nullptr, ss.str().c_str(), L"Launcher", MB_ICONERROR);
        return ERROR_FILE_NOT_FOUND;
    }

    std::vector<std::wstring> args;

    int argc = 0; LPWSTR* argv = CommandLineToArgvW(GetCommandLineW(), &argc);
    if (!argv) return 1;

    if (argc == 1) {                    // Explorer launch → default file
        load_argfile(base / L"stubLauncher.args", args);
    }

    for (int i = 1; i < argc; ++i) {
        std::wstring w = argv[i];
        args.push_back(w);
    }
    LocalFree(argv);

    std::wstring cmd = win_quote(java_exe.wstring());
    for (const auto& a : args) {
        cmd.push_back(L' ');
        cmd += win_quote(a);
    }

    SetEnvironmentVariableW(L"_JAVA_OPTIONS", L"-DstubLauncher=v1");

    DWORD flags = GetConsoleWindow() ? 0 : CREATE_NO_WINDOW;
    STARTUPINFOW si = {};
    si.cb = sizeof(si);
    PROCESS_INFORMATION pi{};
    if (CreateProcessW(java_exe.c_str(), cmd.data(),
        nullptr, nullptr, TRUE,
        flags, nullptr, base.c_str(),
        &si, &pi) == 0)
    {
        MessageBoxW(nullptr, L"Unable to launch java.exe",
            L"Launcher", MB_ICONERROR);
        return (int)GetLastError();
    }

    CloseHandle(pi.hThread);
    WaitForSingleObject(pi.hProcess, INFINITE);

    DWORD ec = 0; GetExitCodeProcess(pi.hProcess, &ec);
    CloseHandle(pi.hProcess);
    return (int)ec;
}

// ------------------------------------------------------------
// Entry point – GUI subsystem
// ------------------------------------------------------------
int WINAPI wWinMain(HINSTANCE, HINSTANCE, PWSTR, int)
{
    enable_dpi_awareness();
    AttachConsole(ATTACH_PARENT_PROCESS);
    return launch_java();
}
