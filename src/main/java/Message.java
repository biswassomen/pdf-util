public class Message {

    private final String text;

    private final boolean isRightAlign;

    public Message(String text, boolean isRightAlign) {
        this.text = text;
        this.isRightAlign = isRightAlign;
    }

    public String getText() {
        return text;
    }

    public boolean isRightAlign() {
        return isRightAlign;
    }
}
