method Abs(x:int) returns (y:int) 
	ensures y >= 0
	ensures (x < 0&& y == -x) || (x >= 0 && y == x)
{
	if x < 0
	{
		return -x;
	} 
	else 
	{
		return x;
	}
}