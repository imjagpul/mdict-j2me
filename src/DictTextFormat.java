public interface DictTextFormat {
	abstract void startTextFormat();
	abstract void stopTextFormat();
	
	abstract void appendHeader(Bytes text);
	abstract void appendItemLeft(Bytes text);
	abstract void appendItemRight(Bytes text);
	abstract void appendLine();
}
