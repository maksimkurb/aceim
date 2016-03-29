package ru.cubly.aceim.client.utils.linq;

public interface KindaLinqRule<T> {

	public boolean match(T t);
}
