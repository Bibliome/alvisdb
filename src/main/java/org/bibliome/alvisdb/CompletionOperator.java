package org.bibliome.alvisdb;

public enum CompletionOperator {
	CONTAINS {
		@Override
		public boolean match(String a, String b) {
			return a.toLowerCase().contains(b.toLowerCase());
		}
	},
	
	STARTS_WITH {
		@Override
		public boolean match(String a, String b) {
			return a.toLowerCase().startsWith(b.toLowerCase());
		}
	};
	
	public abstract boolean match(String a, String b);
}
