
.test.test_add:{	3;  }

.test.test_subtract:{
	1=subtract[2;1]; :()
	}

.test.test_msg:{
	1+1; .assert.fail["Expected 2 but got 3"];
	}

