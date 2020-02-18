import java.io.*
import java.lang.Boolean

private const val EXTINF_TAG = "#EXTINF:"
private const val EXTINF_TVG_NAME = "tvg-name=\""
private const val EXTINF_TVG_ID = "tvg-id=\""
private const val EXTINF_TVG_LOGO = "tvg-logo=\""
private const val EXTINF_TVG_EPGURL = "tvg-epgurl=\""
private const val EXTINF_GROUP_TITLE = "group-title=\""
private const val EXTINF_RADIO = "radio=\""
private const val EXTINF_TAGS = "tags=\""

class PlaylistParser {

    lateinit var items: ArrayList<PlaylistItem>
    var lastItem: PlaylistItem? = null

    fun parse(filePath: String) : ArrayList<PlaylistItem> {
        return parse(FileInputStream(filePath))
    }
    
    fun parse(inputStream: InputStream) : ArrayList<PlaylistItem> {
        items = ArrayList()
        var bufferedReader: BufferedReader? = null
        var line: String? = null
        try {
            bufferedReader = BufferedReader(InputStreamReader(inputStream))
            while (bufferedReader.readLine() != null) {
                line = bufferedReader.readLine()
                try {
                    
                } catch (e: Exception) {
                    lastItem = null
                }
            }
        } catch (rethrow: IOException) {
            throw rethrow
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (ignored: IOException) {
                    
                }
            }
        }
        return items
    }

    private fun parseLine(line: String) {
        var line = line
        line = line.trim { it <= ' ' }
        // EXTINF line
        if (line.startsWith(EXTINF_TAG)) {
            lastItem = parseExtInf(line)
        } else if (!line.isEmpty() && !line.startsWith("#")) {
            if (lastItem == null) {
                lastItem = PlaylistItem()
            }
            lastItem!!.url = line
            items.add(lastItem!!)
            lastItem = null
        } else {
            lastItem = null
        }
    }

    private fun parseExtInf(line: String): PlaylistItem {
        var line = line
        val curEntry = PlaylistItem()
        val buf = StringBuilder(20)
        if (line.length < EXTINF_TAG.length + 1) {
            return curEntry
        }
        // Strip tag
        line = line.substring(EXTINF_TAG.length)
        // Read seconds (may end with comma or whitespace)
        while (line.length > 0) {
            val c = line[0]
            line = if (Character.isDigit(c) || c == '-' || c == '+') {
                buf.append(c)
                line.substring(1)
            } else {
                break
            }
        }
        if (buf.length == 0 || line.isEmpty()) {
            return curEntry
        }
        curEntry.seconds = Integer.valueOf(buf.toString())
        // tvg tags
        while (line.isNotEmpty() && !line.startsWith(",")) {
            line = line.trim { it <= ' ' }
            if (line.startsWith(EXTINF_TVG_NAME) && line.length > EXTINF_TVG_NAME.length) {
                line = line.substring(EXTINF_TVG_NAME.length)
                val i = line.indexOf("\"")
                curEntry.setTvgName(line.substring(0, i))
                line = line.substring(i + 1)
            }
            if (line.startsWith(EXTINF_TVG_LOGO) && line.length > EXTINF_TVG_LOGO.length) {
                line = line.substring(EXTINF_TVG_LOGO.length)
                val i = line.indexOf("\"")
                curEntry.tvgLogo = line.substring(0, i)
                line = line.substring(i + 1)
            }
            if (line.startsWith(EXTINF_TVG_EPGURL) && line.length > EXTINF_TVG_EPGURL.length) {
                line = line.substring(EXTINF_TVG_EPGURL.length)
                val i = line.indexOf("\"")
                curEntry.tvgEpgUrl = line.substring(0, i)
                line = line.substring(i + 1)
            }
            if (line.startsWith(EXTINF_RADIO) && line.length > EXTINF_RADIO.length) {
                line = line.substring(EXTINF_RADIO.length)
                val i = line.indexOf("\"")
                curEntry.isRadio = (Boolean.parseBoolean(line.substring(0, i)))
                line = line.substring(i + 1)
            }
            if (line.startsWith(EXTINF_GROUP_TITLE) && line.length > EXTINF_GROUP_TITLE.length) {
                line = line.substring(EXTINF_GROUP_TITLE.length)
                val i = line.indexOf("\"")
                curEntry.groupTitle = line.substring(0, i)
                line = line.substring(i + 1)
            }
            if (line.startsWith(EXTINF_TVG_ID) && line.length > EXTINF_TVG_ID.length) {
                line = line.substring(EXTINF_TVG_ID.length)
                val i = line.indexOf("\"")
                curEntry.tvgId = line.substring(0, i)
                line = line.substring(i + 1)
            }
            if (line.startsWith(EXTINF_TAGS) && line.length > EXTINF_TAGS.length) {
                line = line.substring(EXTINF_TAGS.length)
                val i = line.indexOf("\"")
                curEntry.tags = line.substring(0, i).split(",").toTypedArray()
                line = line.substring(i + 1)
            }
        }
        // Name
        line = line.trim { it <= ' ' }
        if (line.length > 1 && line.startsWith(",")) {
            line = line.substring(1)
            line = line.trim { it <= ' ' }
            if (!line.isEmpty()) {
                curEntry.name = line
            }
        }
        return curEntry
    }

    class PlaylistItem : Serializable {
        private var _tvgName: String? = null
        private var _name: String? = null
        var tvgLogo: String? = null
        var tvgEpgUrl: String? = null
        var tvgId: String? = null
        var groupTitle: String? = null
        var url: String? = null
        var tags = arrayOfNulls<String>(0)
        var seconds = -1
        var isRadio = false

        fun setTvgName(value: String?) {
            _tvgName = value
        }

        var name: String?
            get() = if (_tvgName != null) _tvgName else _name
            set(value) {
                _name = value
            }

        override fun toString(): String {
            return "$name $url"
        }
    }

}
