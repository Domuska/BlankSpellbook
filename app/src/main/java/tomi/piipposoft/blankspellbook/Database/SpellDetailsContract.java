package tomi.piipposoft.blankspellbook.Database;

import android.provider.BaseColumns;

/**
 * Created by Domu on 08-Sep-15.
 */
public final class SpellDetailsContract {



    public static abstract class SpellDetails implements BaseColumns {

        public static final String TABLE_NAME = "spellDetails";
        public static final String COLUMN_NAME_DETAIL_ID = "detailId";
        public static final String COLUMN_NAME_ATTRIBUTE_TEXT = "attribute";
    }
}
