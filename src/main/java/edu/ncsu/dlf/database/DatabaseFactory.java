package edu.ncsu.dlf.database;

public class DatabaseFactory {

    public static DBAbstraction getDatabase() {
        return new MongoDB();
    }

}
