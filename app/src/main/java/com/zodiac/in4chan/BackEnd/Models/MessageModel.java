package com.zodiac.in4chan.BackEnd.Models;

public class MessageModel {
        private long timestamp;
        private String sender;
        private String receiver;
        private boolean read;
        private String imagePath;
        private String message;
        private String filePath;
        private int rowId;
        private boolean delivery;

        public MessageModel(long timestamp, String sender, String receiver, boolean read, String imagePath, String message, String filePath,int rowId, boolean delivery){
                this.timestamp = timestamp;
                this.sender = sender;
                this.receiver = receiver;
                this.message = message;
                this.filePath = filePath;
                this.imagePath = imagePath;
                this.rowId = rowId;
                this.read = read;
                this.delivery = delivery;
        }

        public MessageModel(){

        }

        public void setRowId(int rowId) {
                this.rowId = rowId;
        }

        public void setTimestamp(long timestamp) {
                this.timestamp = timestamp;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        public void setImagePath(String imagePath) {
                this.imagePath = imagePath;
        }

        public void setFilePath(String filePath) {
                this.filePath = filePath;
        }

        public void setSender(String sender) {
                this.sender = sender;
        }

        public void setRead(boolean read) {
                this.read = read;
        }

        public void setReceiver(String receiver) {
                this.receiver = receiver;
        }

        public int getRowId() {
                return rowId;
        }

        public long getTimestamp() {
                return timestamp;
        }

        public String getFilePath() {
                return filePath;
        }

        public String getMessage() {
                return message;
        }

        public String getImagePath() {
                return imagePath;
        }

        public String getReceiver() {
                return receiver;
        }

        public String getSender() {
                return sender;
        }

        public boolean getRead() {
                return read;
        }

        public boolean isDelivery() {
                return delivery;
        }

        public void setDelivery(boolean delivery) {
                this.delivery = delivery;
        }
}
