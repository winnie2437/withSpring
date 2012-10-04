package jtc.winnie.terminal;

import jtc.winnie.terminal.CashOutEnabled;
import jtc.winnie.terminal.CashSlice;
import jtc.winnie.terminal.Cassette;

public class CashOutCassette extends Cassette implements CashOutEnabled
{
	public CashOutCassette(CashSlice vSlice)
	{
		super(vSlice);
	}
}

