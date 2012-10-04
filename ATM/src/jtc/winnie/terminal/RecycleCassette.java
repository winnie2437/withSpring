package jtc.winnie.terminal;

import jtc.winnie.terminal.CashInEnabled;
import jtc.winnie.terminal.CashOutEnabled;
import jtc.winnie.terminal.CashSlice;
import jtc.winnie.terminal.Cassette;

public class RecycleCassette extends Cassette implements CashOutEnabled,CashInEnabled
{
	public RecycleCassette(CashSlice vSlice)
	{
		super(vSlice);
	}
	
}
