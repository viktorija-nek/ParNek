package utils;

import java.util.Comparator;
import java.util.Set;
import java.util.Map.Entry;

public class ComparatorSize implements Comparator<Entry<String, Set<String>>> {
	@Override
	public int compare(Entry<String, Set<String>> o1, Entry<String, Set<String>> o2) {
		return o1.getValue().size() - o2.getValue().size();
	}
}