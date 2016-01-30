package edu.westga.providerexample.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wlloyd on 1/29/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static DatabaseHandler singleton;

    public static DatabaseHandler getInstance(final Context context) {
        if (singleton == null) {
            singleton = new DatabaseHandler(context);
        }
        return singleton;
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "providerExample";

    private final Context context;

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Person.CREATE_TABLE);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public synchronized Person getPerson(final long id) {
        final SQLiteDatabase db = this.getReadableDatabase();
        final Cursor cursor = db.query(Person.TABLE_NAME,
                Person.FIELDS, Person.COL_ID + " IS ?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor == null || cursor.isAfterLast()) {
            return null;
        }

        Person item = null;
        if (cursor.moveToFirst()) {
            item = new Person(cursor);
        }
        cursor.close();

        return item;
    }

    public synchronized boolean putPerson(final Person person) {
        boolean success = false;
        int result = 0;
        final SQLiteDatabase db = this.getWritableDatabase();

        if (person.id > -1) {
            result += db.update(Person.TABLE_NAME, person.getContent(),
                    Person.COL_ID + " IS ?",
                    new String[] { String.valueOf(person.id) });
        }

        if (result > 0) {
            success = true;
        } else {
            // Update failed or wasn't possible, insert instead
            final long id = db.insert(Person.TABLE_NAME, null,
                    person.getContent());

            if (id > -1) {
                person.id = id;
                success = true;
            }
        }

        if (success) {
            notifyProviderOnPersonChange();
        }

        return success;
    }

    public synchronized int removePerson(final Person person) {
        final SQLiteDatabase db = this.getWritableDatabase();
        final int result = db.delete(Person.TABLE_NAME,
                Person.COL_ID + " IS ?",
                new String[] { Long.toString(person.id) });

        if (result > 0) {
            notifyProviderOnPersonChange();
        }

        return result;
    }

    private void notifyProviderOnPersonChange() {
        context.getContentResolver().notifyChange(
                PersonProvider.URI_PERSONS, null, false);
    }
}
