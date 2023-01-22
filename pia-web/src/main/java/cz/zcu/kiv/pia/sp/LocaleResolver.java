package cz.zcu.kiv.pia.sp;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.List;
import java.util.Locale;

/**
 * trida pro zmenu jazyka
 */
@Component
public class LocaleResolver implements LocaleContextResolver {

    @Override
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        List<String> lang = exchange.getRequest().getQueryParams().get("lang");
        Locale targetLocale = null;
        if (lang != null && !lang.isEmpty()) {
            targetLocale = Locale.forLanguageTag(lang.get(0));
        }
        if (targetLocale == null) {
            targetLocale = Locale.getDefault();
        }
        return new SimpleLocaleContext(targetLocale);
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
        throw new UnsupportedOperationException("Not Supported");
    }
}
