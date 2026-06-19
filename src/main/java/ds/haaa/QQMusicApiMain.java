package ds.haaa;

import com.coloryr.allmusic.server.core.AllMusic;
import com.coloryr.allmusic.server.core.IMusicApi;
import com.coloryr.allmusic.server.core.music.LyricSave;
import com.coloryr.allmusic.server.core.objs.SearchMusicObj;
import com.coloryr.allmusic.server.core.objs.message.ARG;
import com.coloryr.allmusic.server.core.objs.music.SearchPageObj;
import com.coloryr.allmusic.server.core.objs.music.SongInfoObj;
import com.coloryr.allmusic.server.core.saves.MusicListSave;
import com.coloryr.allmusic.server.core.utils.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QQMusicApiMain implements IMusicApi {
    private boolean isUpdate;

    public QQMusicApiMain() {
        QQMusicHttpClient.log("<yellow>正在初始化QQ音乐API");
    }

    @Override
    public String getId() {
        return "qqmusic";
    }

    @Override
    public boolean isBusy() {
        return isUpdate;
    }

    @Override
    public String getMusicId(String arg) {
        if (arg == null) {
            return "";
        }
        if (arg.contains("songmid=")) {
            if (arg.contains("&")) {
                return Function.getString(arg, "songmid=", "&");
            }
            return Function.getString(arg, "songmid=", null);
        }
        if (arg.contains("song/")) {
            String id = Function.getString(arg, "song/", null);
            if (id != null && id.contains(".")) {
                id = Function.getString(id, null, ".");
            }
            if (id != null && id.contains("?")) {
                id = Function.getString(id, null, "?");
            }
            return id == null ? arg : id;
        }
        return arg;
    }

    @Override
    public boolean checkId(String id) {
        return id != null && id.length() >= 10 && id.length() <= 32 && id.matches("[0-9A-Za-z]+");
    }

    @Override
    public SongInfoObj getMusic(String id, String player, boolean isList) {
        id = getMusicId(id);
        QQSong song = QQMusicClient.getSong(id);
        if (song == null) {
            List<QQSong> fallback = QQMusicClient.search(id, 1);
            if (fallback != null && !fallback.isEmpty()) {
                song = fallback.get(0);
            }
        }
        if (song == null) {
            QQMusicHttpClient.log("<red>QQ音乐歌曲信息获取为空：" + id);
            return null;
        }
        String playUrl = QQMusicClient.getPlayUrl(song);
        if (playUrl == null || playUrl.isEmpty()) {
            QQMusicHttpClient.log("<red>QQ音乐正式播放链接为空，歌曲信息返回null：" + song.realId());
            return null;
        }
        boolean trial = false;
        return new SongInfoObj(
                empty(song.singer, "未知歌手"),
                empty(song.name, song.realId()),
                song.realId(),
                null,
                player,
                empty(song.album, "QQ音乐"),
                isList,
                song.lengthMs(),
                song.picUrl(),
                trial,
                null,
                getId()
        );
    }

    @Override
    public SearchPageObj search(String[] name, boolean isDefault) {
        List<SearchMusicObj> resData = new ArrayList<>();
        String keyword = joinKeyword(name, isDefault);
        if (keyword.isEmpty()) {
            QQMusicHttpClient.log("<red>QQ音乐搜索关键字为空");
            return null;
        }
        List<QQSong> songs = QQMusicClient.search(keyword, 30);
        if (songs == null || songs.isEmpty()) {
            QQMusicHttpClient.log("<red>QQ音乐搜索结果为空：" + keyword);
            return null;
        }
        for (QQSong temp : songs) {
            if (temp == null || temp.realId().isEmpty()) {
                continue;
            }
            resData.add(new SearchMusicObj(temp.realId(), empty(temp.name, temp.realId()), empty(temp.singer, "未知歌手"), empty(temp.album, "QQ音乐")));
        }
        if (resData.isEmpty()) {
            QQMusicHttpClient.log("<red>QQ音乐搜索解析后无有效歌曲：" + keyword);
            return null;
        }
        int maxpage = Math.max(1, (resData.size() + 9) / 10);
        return new SearchPageObj(resData, maxpage, getId());
    }

    @Override
    public void setList(String id, Object sender) {
        final Thread thread = new Thread(() -> {
            isUpdate = true;
            try {
                String[] ids = id.split(",");
                List<String> list = new ArrayList<>();
                for (String item : ids) {
                    String temp = getMusicId(item.trim());
                    if (checkId(temp)) {
                        list.add(temp);
                    }
                }
                if (!list.isEmpty()) {
                    MusicListSave.addIdleList(list, getId());
                    AllMusic.side.sendMessageTask(sender, AllMusic.getMessage().musicPlay.listMusic.get.replace(ARG.name, "QQMusic"));
                } else {
                    AllMusic.side.sendMessageTask(sender, "QQ音乐歌单暂只支持逗号分隔的songmid列表");
                }
            } catch (Exception e) {
                QQMusicHttpClient.log("<red>QQ音乐列表获取错误");
                if (QQSong.debug) {
                    e.printStackTrace();
                }
            }
            isUpdate = false;
        }, "AllMusic_QQMusic_setList");
        thread.start();
    }

    @Override
    public LyricSave getLyric(String id) {
        LyricSave save = new LyricSave();
        String lyric = QQMusicClient.getLyricText(getMusicId(id));
        Map<Long, com.coloryr.allmusic.server.core.objs.music.LyricItemObj> map = QQMusicLyricDecoder.parse(lyric);
        if (!map.isEmpty()) {
            save.setHaveLyric(AllMusic.getConfig().sendLyric);
            save.setLyric(map);
        }
        return save;
    }

    @Override
    public String getPlayUrl(String id) {
        return QQMusicClient.getPlayUrl(getMusicId(id));
    }

    private static String joinKeyword(String[] name, boolean isDefault) {
        if (name == null || name.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = isDefault ? 0 : 1; i < name.length; i++) {
            if (name[i] != null && !name[i].trim().isEmpty()) {
                builder.append(name[i].trim()).append(" ");
            }
        }
        if (builder.length() == 0) {
            return "";
        }
        return builder.substring(0, builder.length() - 1);
    }

    private static String empty(String value, String def) {
        return value == null || value.isEmpty() ? def : value;
    }
}
