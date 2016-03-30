package ru.cubly.aceim.app.dataentity.listeners;

import ru.cubly.aceim.api.dataentity.FileProgress;

public interface IHasFileProgress {
	void onFileProgress(FileProgress progress);
}
