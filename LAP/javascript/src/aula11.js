function Succ(first) {
	this.f = first;
	this.c = first;
	this.first = function(){
		this.c = this.f;
		return this.c;
	}
	this.curr = function() {
		return this.c;
	}
	this.print = function(n){
		this.first();
		result = [];
		for (var i = 0; i < n; i++) {
			result.push(this.curr());
			this.next();
		}
		return result;
	}
	this.at = function(i){
		this.first();
		for (var a = 0; a < i-1; a++) {
			this.next();
		}
		return this.curr();
	}
	this.next = function(){
		return;
	}
}

function Arith(first, inc){
	Succ.apply(this, [first]);
	this.inc = inc;
	this.next = function () {
		this.c = this.c + this.inc;
		return this.c;
	}
	this.at = function(i) {
		this.c = this.f + i*this.inc;
		return this.c;
	}
}

function Geo(first, base) {
	Succ.apply(this, [first]);
	this.base = base;
	this.next = function () {
		this.c = this.c*this.base;
		return this.c;
	}
}

function Const(first){
	Succ.apply(this, [first]);
	this.next = function(){
		return this.c;
	}
}

function Sum(f_succ, s_succ) {
	Succ.apply(this, [f_succ.f+s_succ.f]);
	this.f_succ = f_succ;
	this.s_succ = s_succ;
	this.first = function(){
		this.c = this.f;
		this.s_succ.first();
		this.f_succ.first();
		return this.c;
	}
	this.next = function(){
		this.c = (this.f_succ.next() + this.s_succ.next());
		return this.c;
	}
}

function Alternate(f_succ, s_succ){
	Succ.apply(this, [f_succ.f]);
	this.f_succ = f_succ;
	this.s_succ = s_succ;
	this.count = 0;
	this.first = function(){
		this.c = this.f;
		this.f_succ.first();
		this.s_succ.first();
		return this.c;
	}
	this.next = function(){
		if(this.count%2 == 0){
			this.c = this.s_succ.curr();
			this.s_succ.next();
		}else{
			this.c = this.f_succ.next();
		}
		this.count++;
		return this.c;
	}
}

function Filter(succ, filter) {
	this.succ = succ;
	this.filter = filter;
	while(this.succ.curr()%filter != 0)
		this.succ.next();
	Succ.apply(this, [this.succ.curr()]);
	this.first = function() {
		this.succ.first();
		while(this.succ.curr()%filter != 0)
			this.succ.next();
		this.c = this.f;
	}
	this.next = function(){
		this.c = this.succ.next();
		while(this.succ.curr()%filter != 0)
			this.c = this.succ.next();
		return this.c;
	}
}

function Fib(first, second){
	Succ.apply(this, [first]);
	this.second = second;
	this.prev = first;
	this.count = 0;
	this.first = function(){
		this.c = this.f;
		this.prev = this.f;
		this.count = 0;
	}
	this.next = function(){
		if(count == 0){
			this.c = this.second;
			this.prev = this.f;
		} else {
			this.c = this.c + this.prev;
			this.prev = this.c - this.prev;
		}
		count++;
		return this.c;
	}
}