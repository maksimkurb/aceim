package ru.cubly.aceim.app.utils.linq;

public interface KindaLinqRule<T> {

	public boolean match(T t);
}
