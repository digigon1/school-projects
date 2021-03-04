function square(number){
	return number*number;
}

function isPrime(number){
	if(number == 1){
		return "not a prime number"
	}
	for (var i = 2; i <= number/2; i++) {
		if(number%i == 0){
			return "not a prime number";
		}
	}
	return "a prime number";
}

function main(){
	var number = prompt("Enter number to test if prime:","1");
	
	return number+" is "+isPrime(number); //program output goes here
}