package com.example.maxstorage.Login;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.maxstorage.DBConection_Conexion.DBConnection;
import com.example.maxstorage.Login.User;

public class UserDAO {
    private final SQLiteDatabase db;

    public UserDAO(Context ctx) {
        db = DBConnection.getInstance(ctx).getDatabase();
    }

    /** Inserta un nuevo usuario y retorna su ID */
    public long insert(User u) {
        ContentValues cv = new ContentValues();
        cv.put("username",      u.getUsername());
        cv.put("password_hash", u.getPasswordHash());
        cv.put("owner_name",    u.getOwnerName());
        cv.put("email",         u.getEmail());
        return db.insert("system_users", null, cv);
    }

    /** Busca un usuario por su nombre de usuario */
    public User findByUsername(String username) {
        Cursor c = db.query(
                "system_users",
                null,
                "username = ?",
                new String[]{ username },
                null, null, null
        );
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        User u = mapCursorToUser(c);
        c.close();
        return u;
    }

    /** Busca un usuario por su ID */
    public User findById(int userId) {
        Cursor c = db.query(
                "system_users",
                null,
                "user_id = ?",
                new String[]{ String.valueOf(userId) },
                null, null, null
        );
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        User u = mapCursorToUser(c);
        c.close();
        return u;
    }

    /** Mapea la fila actual del cursor a un objeto User */
    private User mapCursorToUser(Cursor c) {
        return new User(
                c.getInt(c.getColumnIndexOrThrow("user_id")),
                c.getString(c.getColumnIndexOrThrow("username")),
                c.getString(c.getColumnIndexOrThrow("password_hash")),
                c.getString(c.getColumnIndexOrThrow("owner_name")),
                c.getString(c.getColumnIndexOrThrow("email")),
                c.getString(c.getColumnIndexOrThrow("created_at"))
        );
    }
}
