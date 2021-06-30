package org.dstadler.commons.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.dstadler.commons.testing.TestHelpers;

public class IPTest {
	@Test
	public void testIP() {
		checkOneIp(0,0,0,0,0);
		checkOneIp(1,1,1,1,16843009);
		checkOneIp(128,0,0,0, 2147483648L);
		checkOneIp(128,128,128,128, 2155905152L);
		checkOneIp(129,129,129,129, 2172748161L);
		checkOneIp(254,254,254,254, 4278124286L);
		checkOneIp(255,255,255,255, 4294967295L);
	}

	private void checkOneIp(int i1, int i2, int i3, int i4, long sum) {
		IP ip = new IP(i1, i2, i3, i4);
		assertEquals(sum, ip.getLong());
		IP ip2 = new IP(ip.getLong());
		assertEquals(i1, ip2.i1);
		assertEquals(i2, ip2.i2);
		assertEquals(i3, ip2.i3);
		assertEquals(i4, ip2.i4);
	}

	@Test
	public void testEqualsHashCode() {
		IP ip = new IP(1, 2, 3, 4);
		IP equ = new IP(1, 2, 3, 4);
		IP notEqu = new IP(1, 2, 3, 5);

		TestHelpers.HashCodeTest(ip, equ);
		TestHelpers.EqualsTest(ip, equ, notEqu);

		notEqu = new IP(0,2,3,4);
		TestHelpers.EqualsTest(ip, equ, notEqu);

		notEqu = new IP(255,2,3,4);
		TestHelpers.EqualsTest(ip, equ, notEqu);

		notEqu = new IP(1,255,3,4);
		TestHelpers.EqualsTest(ip, equ, notEqu);

		notEqu = new IP(1,2,255,4);
		TestHelpers.EqualsTest(ip, equ, notEqu);

		notEqu = new IP(1,2,3,255);
		TestHelpers.EqualsTest(ip, equ, notEqu);
	}
}
