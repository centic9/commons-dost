package org.dstadler.commons.http;

public class IP {
	public int i1;
	public int i2;
	public int i3;
	public int i4;

	private int returnCode;

	public IP(int i1, int i2, int i3, int i4) {
		super();
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
		this.i4 = i4;
	}

	public IP(long ip) {
		super();
		long i = ip;

		this.i4 = (int)(i % 256);
		i>>=8;

		this.i3 = (int)(i % 256);
		i>>=8;

		this.i2 = (int)(i % 256);
		i>>=8;

		this.i1 = (int)i;
	}

	public long getLong() {
		long ip = i1<<8;
		ip+= i2;
		ip<<=8;
		ip+=i3;
		ip<<=8;
		ip+=i4;

		return ip;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i1;
		result = prime * result + i2;
		result = prime * result + i3;
		result = prime * result + i4;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IP other = (IP) obj;
		if (i1 != other.i1) {
			return false;
		}
		if (i2 != other.i2) {
			return false;
		}
		if (i3 != other.i3) {
			return false;
		}
		if (i4 != other.i4) {
			return false;
		}
		return true;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

}
