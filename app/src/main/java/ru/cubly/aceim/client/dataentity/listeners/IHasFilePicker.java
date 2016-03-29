package ru.cubly.aceim.client.dataentity.listeners;

import ru.cubly.aceim.client.MainActivity;
import ru.cubly.aceim.client.dataentity.ActivityResult;

public interface IHasFilePicker {

	void onFilePicked(ActivityResult result, MainActivity activity);
}
