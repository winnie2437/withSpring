package jtc.winnie.terminal;

import jtc.winnie.terminal.CashInEnabled;
import jtc.winnie.terminal.CashSlice;
import jtc.winnie.terminal.Cassette;

public class CashInCassette extends Cassette implements CashInEnabled
{
	public CashInCassette(CashSlice vSlice)
	{
		super(vSlice);
	}
	
}
