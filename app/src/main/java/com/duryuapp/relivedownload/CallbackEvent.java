package com.duryuapp.relivedownload;

public interface CallbackEvent {  
	public void title(String title);
	public void fileInfo(String fileurl, String filename, int filesize);
    public void download(int cur, int total);
    public void downloadCompleted();
    public void downloadCanceled();
    public void downloadFailed();
}
