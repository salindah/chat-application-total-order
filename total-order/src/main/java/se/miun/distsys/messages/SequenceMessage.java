package se.miun.distsys.messages;

public class SequenceMessage extends Message {


	private static final long serialVersionUID = -5173398487182472017L;
	
	private Long sequenceNumber;
	
	private Long uniqueMessageId;

	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Long getUniqueMessageId() {
		return uniqueMessageId;
	}

	public void setUniqueMessageId(Long uniqueMessageId) {
		this.uniqueMessageId = uniqueMessageId;
	}
}
