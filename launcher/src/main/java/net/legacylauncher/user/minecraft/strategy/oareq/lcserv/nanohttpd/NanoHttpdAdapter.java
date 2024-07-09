package net.legacylauncher.user.minecraft.strategy.oareq.lcserv.nanohttpd;

import fi.iki.elonen.NanoHTTPD;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.exceptions.ParseException;
import net.legacylauncher.user.minecraft.strategy.oareq.CodeRequestCancelledException;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import net.legacylauncher.user.minecraft.strategy.oareq.lcserv.LocalServerSelectedConfiguration;

import java.net.URI;

@Slf4j
class NanoHttpdAdapter extends NanoHTTPD {
    private final String expectedState;
    private final LockExchange lock;
    private final OAuthUrlParser urlParser;
    private final URI redirectUrl;
    private final String successRedirectUrl;

    NanoHttpdAdapter(LocalServerSelectedConfiguration configuration,
                     LockExchange lock,
                     OAuthUrlParser urlParser,
                     URI redirectUrl, String successRedirectUrl) {
        super(configuration.getHost(), configuration.getPort());
        this.expectedState = configuration.getState();
        this.lock = lock;
        this.urlParser = urlParser;
        this.redirectUrl = redirectUrl;
        this.successRedirectUrl = successRedirectUrl;
    }

    @Override
    public Response serve(IHTTPSession session) {
        log.debug("New request: {}", session.getUri());
        if (!session.getMethod().equals(Method.GET)) {
            log.debug("Not a GET request");
            return badRequest();
        }
        if (session.getUri().equals(redirectUrl.getPath())) {
            log.debug("Not redirect url");
            return badRequest();
        }
        if (expectedState != null) {
            if (!session.getParameters().containsKey("state")) {
                log.debug("No \"state\" parameter");
                return badRequest();
            }
            String state = session.getParameters().get("state").get(0);
            if (!state.equals(expectedState)) {
                log.warn("Expected state: {}, but got: {}", expectedState, state);
                return badRequest();
            }
        }
        String code;
        try {
            code = urlParser.parseAndValidate(session.getUri() + "?" + session.getQueryParameterString());
        } catch (ParseException e) {
            log.debug("Not valid request");
            return badRequest();
        } catch (CodeRequestCancelledException e) {
            log.info("Cancelled");
            return handleCancel(e);
        } catch (MicrosoftOAuthCodeRequestException e) {
            log.info("Returned an error");
            return handleError(e);
        }
        return handleCode(code);
    }

    private Response handleError(MicrosoftOAuthCodeRequestException e) {
        lock.unlockWithError(e);
        return error();
    }

    private Response handleCancel(CodeRequestCancelledException e) {
        lock.unlockWithError(e);
        return cancelled();
    }

    private Response handleCode(String code) {
        lock.unlock(new MicrosoftOAuthExchangeCode(code, redirectUrl));
        return ok();
    }

    private Response ok() {
        boolean redirect = successRedirectUrl != null;
        Response response = newResponse(
                redirect ? Response.Status.TEMPORARY_REDIRECT : Response.Status.OK,
                "Success"
        );
        if (redirect) {
            response.addHeader("Location", successRedirectUrl);
        }
        return response;
    }

    private Response error() {
        return newResponse(Response.Status.BAD_REQUEST, "Something went wrong");
    }

    private Response cancelled() {
        return newResponse(Response.Status.BAD_REQUEST, "Cancelled");
    }

    private Response badRequest() {
        return newResponse(Response.Status.BAD_REQUEST, "Bad Request");
    }

    private static Response newResponse(Response.IStatus status, String text) {
        return newFixedLengthResponse(status, NanoHTTPD.MIME_PLAINTEXT, text);
    }
}
