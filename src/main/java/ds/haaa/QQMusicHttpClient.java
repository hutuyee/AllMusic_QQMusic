package ds.haaa;

import com.coloryr.allmusic.libs.org.apache.hc.client5.http.classic.methods.HttpGet;
import com.coloryr.allmusic.libs.org.apache.hc.client5.http.classic.methods.HttpPost;
import com.coloryr.allmusic.libs.org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import com.coloryr.allmusic.libs.org.apache.hc.core5.http.ContentType;
import com.coloryr.allmusic.libs.org.apache.hc.core5.http.HttpEntity;
import com.coloryr.allmusic.libs.org.apache.hc.core5.http.io.entity.EntityUtils;
import com.coloryr.allmusic.libs.org.apache.hc.core5.http.io.entity.StringEntity;
import com.coloryr.allmusic.server.core.AllMusic;
import com.coloryr.allmusic.server.core.music.MusicHttpClient;
import com.coloryr.allmusic.server.core.objs.CookieObj;
import com.coloryr.allmusic.server.core.objs.HttpResObj;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class QQMusicHttpClient {
    public static final String MUSICU_URL = "https://u.y.qq.com/cgi-bin/musicu.fcg";
    public static final String LYRIC_URL = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg";
    public static final String SEARCH_OLD_URL = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp";

    private static final String REFERER = "https://y.qq.com/";
    private static final String ORIGIN = "https://y.qq.com";
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    public static HttpResObj get(String url) {
        try {
            HttpGet request = new HttpGet(url);
            setHeaders(request);
            log("<gray>QQ音乐GET: " + url);
            return execute(request, "QQ音乐GET请求失败：" + url);
        } catch (Exception e) {
            log("<red>QQ音乐GET请求失败：" + url);
            if (QQSong.debug) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static HttpResObj postJson(String json) {
        return postJson(MUSICU_URL, json);
    }

    public static HttpResObj postJson(String url, String json) {
        try {
            HttpPost request = new HttpPost(url);
            setHeaders(request);
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            log("<gray>QQ音乐POST: " + url);
            log("<gray>QQ音乐POST Body: " + cut(json, 1200));
            return execute(request, "QQ音乐POST请求失败：" + url);
        } catch (Exception e) {
            log("<red>QQ音乐POST请求失败：" + url);
            if (QQSong.debug) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static void setHeaders(com.coloryr.allmusic.libs.org.apache.hc.core5.http.HttpMessage request) {
        request.setHeader("User-Agent", UA);
        request.setHeader("Referer", REFERER);
        request.setHeader("Origin", ORIGIN);
        request.setHeader("Accept", "application/json, text/plain, */*");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        request.setHeader("Connection", "keep-alive");

        String cookie = buildCookieHeader();
        if (!cookie.isEmpty()) {
            request.setHeader("Cookie", cookie);
            log("<gray>QQ音乐Cookie已注入，cookie：" + cookie);
        } else {
            log("<yellow>QQ音乐Cookie为空，将以未登录状态请求");
        }
    }

    private static String buildCookieHeader() {
        try {
            List<CookieObj> cookies = AllMusic.cookie;
            if (cookies == null || cookies.isEmpty()) {
                return "";
            }

            StringBuilder builder = new StringBuilder();

            appendCookie(builder, "login_type", getCookieValue("login_type", "1"));
            appendCookie(builder, "tmeLoginType", getCookieValue("tmeLoginType", "2"));
            appendCookie(builder, "euin", getCookieValue("euin", ""));
            appendCookie(builder, "RK", getCookieValue("RK", ""));
            appendCookie(builder, "_qpsvr_localtk", getCookieValue("_qpsvr_localtk", "0.41530072345568325"));
            appendCookie(builder, "music_ignore_pskey", getCookieValue("music_ignore_pskey", ""));
            appendCookie(builder, "psrf_qqrefresh_token", getCookieValue("psrf_qqrefresh_token", ""));

            String uin = getCookieValue("uin", "");
            if (uin.isEmpty()) {
                uin = getCookieValue("media_p_uin", "");
            }
            appendCookie(builder, "uin", uin);

            appendCookie(builder, "pgv_pvid", getCookieValue("pgv_pvid", ""));
            appendCookie(builder, "pgv_info", getCookieValue("pgv_info", ""));
            appendCookie(builder, "fqm_sessionid", getCookieValue("fqm_sessionid", ""));
            appendCookie(builder, "fqm_pvqid", getCookieValue("fqm_pvqid", ""));
            appendCookie(builder, "psrf_access_token_expiresAt", getCookieValue("psrf_access_token_expiresAt", ""));
            appendCookie(builder, "psrf_musickey_createtime", getCookieValue("psrf_musickey_createtime", ""));
            appendCookie(builder, "psrf_qqaccess_token", getCookieValue("psrf_qqaccess_token", ""));
            appendCookie(builder, "psrf_qqopenid", getCookieValue("psrf_qqopenid", ""));
            appendCookie(builder, "psrf_qqunionid", getCookieValue("psrf_qqunionid", ""));
            appendCookie(builder, "ptcz", getCookieValue("ptcz", ""));
            appendCookie(builder, "qm_keyst", getCookieValue("qm_keyst", ""));
            appendCookie(builder, "qqmusic_key", getCookieValue("qqmusic_key", ""));
            appendCookie(builder, "ts_last", getCookieValue("ts_last", ""));
            appendCookie(builder, "ts_uid", getCookieValue("ts_uid", ""));
            appendCookie(builder, "wxunionid", getCookieValue("wxunionid", ""));
            appendCookie(builder, "wxrefresh_token", getCookieValue("wxrefresh_token", ""));
            appendCookie(builder, "wxopenid", getCookieValue("wxopenid", ""));

            return builder.toString();
        } catch (Exception e) {
            log("<red>QQ音乐Cookie读取失败");
            if (QQSong.debug) {
                e.printStackTrace();
            }
            return "";
        }
    }

    private static void appendCookie(StringBuilder builder, String name, String value) {
        if (name == null || name.isEmpty()) {
            return;
        }

        if (builder.length() > 0) {
            builder.append("; ");
        }

        builder.append(name).append("=");
        if (value != null) {
            builder.append(value);
        }
    }

    public static String getCookieValue(String name, String def) {
        try {
            if (name == null || name.isEmpty() || AllMusic.cookie == null) {
                return def;
            }
            for (CookieObj cookie : AllMusic.cookie) {
                if (cookie != null && name.equals(cookie.name)) {
                    return cookie.value == null || cookie.value.isEmpty() ? def : cookie.value;
                }
            }
        } catch (Exception ignored) {
        }
        return def;
    }

    public static String getUin() {
        String uin = getCookieValue("uin", "");
        if (!uin.isEmpty()) {
            return uin;
        }
        uin = getCookieValue("media_p_uin", "");
        return uin.isEmpty() ? "0" : uin;
    }

    private static HttpResObj execute(
            com.coloryr.allmusic.libs.org.apache.hc.client5.http.classic.methods.HttpUriRequestBase request,
            String errorMsg
    ) {
        try (CloseableHttpResponse response = MusicHttpClient.client.execute(request)) {
            int httpCode = response.getCode();
            HttpEntity entity = response.getEntity();

            if (entity == null) {
                log("<red>QQ音乐返回空实体，HTTP=" + httpCode);
                return null;
            }

            String body = read(entity.getContent());
            EntityUtils.consume(entity);

            boolean ok = httpCode >= 200 && httpCode < 300;
            log("<gray>QQ音乐HTTP=" + httpCode + " 返回：" + cut(body, 1200));

            if (!ok) {
                log("<red>QQ音乐服务器返回错误：" + cut(body, 1200));
            }

            return new HttpResObj(body, ok);
        } catch (Exception e) {
            log("<red>" + errorMsg);
            if (QQSong.debug) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static String read(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        inputStream.close();
        return result.toString(StandardCharsets.UTF_8.toString());
    }

    public static String enc(String value) throws Exception {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8.toString());
    }

    public static String cut(String value, int max) {
        if (value == null) {
            return "null";
        }
        return value.length() > max ? value.substring(0, max) : value;
    }

    public static void log(String msg) {
        if (!QQSong.debug) {
            return;
        }
        AllMusic.log.data("<light_purple>[AllMusic3]" + msg);
    }
}