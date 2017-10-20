package fr.inra.maiage.bibliome.alvisdb;

import java.util.Comparator;

public interface ADBElement {
	AlvisDB getAlvisDB();
	String getId();
	String getName();
	
	public static enum NameComparator implements Comparator<ADBEntity> {
		INSTANCE;
		
		@Override
		public int compare(ADBEntity o1, ADBEntity o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
}
