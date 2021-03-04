let rec nat n =
	if n = 0
	then []
	else (n-1)::nat (n-1)
;;