package com.unical.pixelflow.model;


public class UserDatabase extends Database {
        private static UserDatabase instance = null;

        public static UserDatabase getInstance() {
            if (instance == null) {
                instance = new UserDatabase();
            }
            return instance;
        }

        public UserDatabase() {
            super("data/user_dbs/" + DataPF.dbUser.getId() + ".db");
        }
}
