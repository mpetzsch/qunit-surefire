
/ find all tests - must be in .test namespace and start test_
.qunit.getTests:{
    $[`test in key`;`$".test.",/:string f where (f:system["f .test"]) like "test_*";()]
    }

/ load a file/directory
.qunit.load:{[path]
    if[0<count raze path;{system "l ",x; } each path];
    }

.qunit.initialise:{
    .qunit.args:.Q.opt .z.x;
    .qunit.testFile:.qunit.args[`testFile];
    .qunit.sourceFiles:.qunit.args[`sourceFiles];
    .qunit.dependencies:.qunit.args[`dependencies];
    .qunit.load each (.qunit.sourceFiles;.qunit.dependencies;.qunit.testFile);
    .qunit.getTests[]
    }

.qunit.runTest:{[test]
    res:@[value test;`;{ $[x like "ASSERT-FAIL:*";12_x;'x]}];
    / must be empty list (success)
    if[not type[res] in 0 10h;'`$"Bad return type ",string[type[res]],". Expect empty list (success). Got: ",-3!res];
    res
    }

.z.ts:{
    / if noone is connected then suicide - setup after first call
    .z.ts:{if[0=count[.z.W];show string[system"p"],": dying - no connections"; exit 0];};
    }

/ set suicide timer
system "t 5000";
/show "testRunner available @ ",string[.z.h],":",string system "p";

\d .assert

fail:{'`$"ASSERT-FAIL:",x};
equal:{ if[not x~y;fail[z]] }
notEqual:{ if[x~y;fail[z]] }
greaterThan:{ if[not x>y;fail[z]] }
lessThan:{ if[not x<y;fail[z]] }
sameType:{ if[not type[x]=type[y];fail[z]] }

\d .