#define Publisher "Legacy Launcher Team"
#define URL "https://llaun.ch"

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={#Id}
AppName={#Name}
AppVersion=rolling
VersionInfoVersion={#Version}
AppPublisher={#Publisher}
AppPublisherURL={#URL}
AppSupportURL={#URL}
AppUpdatesURL={#URL}
;AlwaysShowDirOnReadyPage=no
AlwaysUsePersonalGroup=yes
ArchitecturesInstallIn64BitMode=x64
DefaultDirName={userappdata}\.tlauncher\{#Branch}\Minecraft
UsePreviousAppDir=no
DisableProgramGroupPage=yes
DisableDirPage=no
DisableWelcomePage=no
ExtraDiskSpaceRequired=536870912
PrivilegesRequired=lowest
OutputBaseFilename=LegacyLauncher_{#Branch}_Installer
;Compression=none
Compression=lzma2/normal
SetupIconFile=icons/icon.ico
SolidCompression=yes
WizardStyle=modern
WizardSmallImageFile=icons\100.bmp,icons\125.bmp,icons\150.bmp,icons\175.bmp,icons\200.bmp,icons\225.bmp,icons\250.bmp
WizardImageFile=images\100.bmp,images\125.bmp,images\150.bmp,images\175.bmp,images\200.bmp,images\225.bmp,images\250.bmp

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl,lang-en.isl"; InfoBeforeFile: "welcome-en.rtf"
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl,lang-ru.isl"; InfoBeforeFile: "welcome-ru.rtf"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}";

[Files]
Source: "files\common\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "files\x64\*"; Check: Is64BitInstallMode; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "files\x86\*"; Check: not Is64BitInstallMode; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "files\common\tl.properties"; DestDir: "{app}"; AfterInstall: ExpandLauncherProperties(); Flags: ignoreversion;
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[UninstallDelete]
Type: filesandordirs; Name: "{app}\jre"
Type: filesandordirs; Name: "{app}\launcher"

[Icons]
Name: "{userprograms}\{#Name}"; Filename: "{app}\TL.exe"
Name: "{autodesktop}\{#Name}"; Filename: "{app}\TL.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\TL.exe"; Description: "{cm:LaunchProgram,{#StringChange(Name, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[CustomMessages]
english.UnicodeModeNonASCIIPathError=Please change installation path to the one that only contains English (ASCII) characters.%n%nYour system uses Unicode mode, which is not well-supported by Java at the moment.
russian.UnicodeModeNonASCIIPathError=Пожалуйста выберите такой путь, в котором не будет русских символов.%n%nВаша система использует режим Unicode для всех приложений. Этот режим не поддерживается приложениями на Java.

[Code]
procedure ExpandLauncherProperties();
var
  AppDir: String;
begin
  AppDir := ExpandConstant('{app}');
  StringChangeEx(AppDir, '\', '\\', true);
  SaveStringsToUTF8File(ExpandConstant(CurrentFileName), [
    'minecraft.gamedir='+ AppDir +'\\game' + #13#10
    'minecraft.jre.dir='+ AppDir +'\\jre' + #13#10
  ], True);
end;

function IsUnicodeMode(): Boolean;
var
  ACP: String;
begin
  RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SYSTEM\CurrentControlSet\Control\Nls\CodePage', 'ACP', ACP);
  Result := (ACP = '65001');
end;

function IsASCIIOnly(const Value: string): Boolean;
var
  I: Integer;
begin
  Result := False;
  for i := 1 to Length(Value) do
    if (Ord(Value[i]) > $007F) then
      Exit;
  Result := True;
end;

function NextButtonClick(PageId: Integer): Boolean;
begin
  Result := True;
  // If Windows is running in Unicode mode, check if {app} path is ASCII-only
  if (PageID = wpSelectDir) and (IsUnicodeMode()) and (not IsASCIIOnly(WizardForm.DirEdit.Text)) then
  begin
    Result := False;
    MsgBox(CustomMessage('UnicodeModeNonASCIIPathError'), mbError, MB_OK);
  end;
end;
