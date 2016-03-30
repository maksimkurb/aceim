package ru.cubly.aceim.app.dataentity.listeners;

import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.dataentity.ActivityResult;

public interface IHasFilePicker {

	void onFilePicked(ActivityResult result, OldMainActivity activity);
}
