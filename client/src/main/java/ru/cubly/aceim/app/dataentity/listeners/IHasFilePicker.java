package ru.cubly.aceim.app.dataentity.listeners;

import ru.cubly.aceim.app.MainActivity;
import ru.cubly.aceim.app.dataentity.ActivityResult;

public interface IHasFilePicker {

	void onFilePicked(ActivityResult result, MainActivity activity);
}
