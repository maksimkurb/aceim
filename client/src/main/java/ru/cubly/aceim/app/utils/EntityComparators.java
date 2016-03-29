package ru.cubly.aceim.app.utils;

import java.util.Comparator;

import ru.cubly.aceim.api.dataentity.Buddy;

public final class EntityComparators {

	private EntityComparators() {}
	
	public enum BuddyNameComparator implements Comparator<Buddy> {
		INSTANCE;

		@Override
		public int compare(Buddy lhs, Buddy rhs) {
			return lhs.compareTo(rhs);
		}		
	}
}
