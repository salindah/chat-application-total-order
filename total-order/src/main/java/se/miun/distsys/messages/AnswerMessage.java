package se.miun.distsys.messages;

public class AnswerMessage extends Message{

	private static final long serialVersionUID = 4561498705303469739L;

	public AnswerMessage(Long userId) {
		this.userId = userId;
	}
	
	public String getMessage() {
		return "Answer from : " + this.userId;
	}
}
