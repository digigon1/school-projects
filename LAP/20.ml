fun x -> fun n -> x + n;;

fun f -> fun n -> 1 + f (n+1);;

let f n fl str =
	ignore(fl = fl +. 0.3);
	ignore(+ n);
	ignore(str ^ str);
	'u'
;;