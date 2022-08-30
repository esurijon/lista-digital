package com.surix.ld.util;


public class Pair<T, V> {
	private T first;
	private V second;

	public Pair(T first, V second) {
		super();
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public void setFirst(T first) {
		this.first = first;
	}

	public V getSecond() {
		return second;
	}

	public void setSecond(V second) {
		this.second = second;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair<?, ?>))
			return false;
		Pair<?, ?> pair = (Pair) o;
		return eq(this.getFirst(), pair.getFirst()) && eq(this.getSecond(), pair.getSecond());
	}

	@Override
	public int hashCode() {
		return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
	}

	private static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
}
