package Game.template;

import java.util.ArrayList;
import java.util.List;

public class Level {
	public static final List<Level> entry = new ArrayList<>();
	public short level;
	public long exp;
	public short tiemnang;
	public short kynang;

	public static short get_tiemnang_by_level(int level) {
		short param = 0;
		for (int i = 0; i < level; i++) {
			param += Level.entry.get(i).tiemnang;
		}
		return param;
	}

	public static short get_kynang_by_level(int level) {
		short param = 0;
		for (int i = 0; i < level; i++) {
			param += Level.entry.get(i).kynang;
		}
		return param;
	}
}
