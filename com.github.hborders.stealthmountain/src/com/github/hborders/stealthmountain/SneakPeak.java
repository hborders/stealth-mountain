package com.github.hborders.stealthmountain;

public class SneakPeak implements Comparable<SneakPeak> {
	public final String sneakPeakUser;
	public final Long sneakPeakUserId;
	public final Long sneakPeakTweetId;
	public final String sneakPeakText;

	public SneakPeak(String sneakPeakUser, Long sneakPeakUserId,
			Long sneakPeakTweetId, String sneakPeakText) {
		super();
		this.sneakPeakUser = sneakPeakUser;
		this.sneakPeakUserId = sneakPeakUserId;
		this.sneakPeakTweetId = sneakPeakTweetId;
		this.sneakPeakText = sneakPeakText;
	}

	@Override
	public int compareTo(SneakPeak thatSneakPeak) {
		return new Long(sneakPeakTweetId)
				.compareTo(thatSneakPeak.sneakPeakTweetId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sneakPeakText == null) ? 0 : sneakPeakText.hashCode());
		result = prime
				* result
				+ ((sneakPeakTweetId == null) ? 0 : sneakPeakTweetId.hashCode());
		result = prime * result
				+ ((sneakPeakUser == null) ? 0 : sneakPeakUser.hashCode());
		result = prime * result
				+ ((sneakPeakUserId == null) ? 0 : sneakPeakUserId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SneakPeak other = (SneakPeak) obj;
		if (sneakPeakText == null) {
			if (other.sneakPeakText != null)
				return false;
		} else if (!sneakPeakText.equals(other.sneakPeakText))
			return false;
		if (sneakPeakTweetId == null) {
			if (other.sneakPeakTweetId != null)
				return false;
		} else if (!sneakPeakTweetId.equals(other.sneakPeakTweetId))
			return false;
		if (sneakPeakUser == null) {
			if (other.sneakPeakUser != null)
				return false;
		} else if (!sneakPeakUser.equals(other.sneakPeakUser))
			return false;
		if (sneakPeakUserId == null) {
			if (other.sneakPeakUserId != null)
				return false;
		} else if (!sneakPeakUserId.equals(other.sneakPeakUserId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SneakPeak [sneakPeakUser=" + sneakPeakUser
				+ ", sneakPeakUserId=" + sneakPeakUserId
				+ ", sneakPeakTweetId=" + sneakPeakTweetId + ", sneakPeakText="
				+ sneakPeakText + "]";
	}
}
