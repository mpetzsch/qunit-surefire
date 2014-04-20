
/ find all tests - must be in .test namespace and start test_
.qunit.getTests:{
    $[`test in key`;`$".test.",/:string f where (f:system["f .test"]) like "test_*";()]
    }

/ load a file/directory
.qunit.load:{[path]

    }

.qunit.initialise:{
    .qunit.args:.Q.opt .z.x;
    .qunit.testFile:.qunit.args[`testFile];
    .qunit.sourceFiles:.qunit.args[`sourceFiles];
    .qunit.dependencies:.qunit.args[`dependencies];
    .qunit.load each (.qunit.sourceFiles;.qunit.dependencies;.qunit.testFile);
    .qunit.getTests[]
    }

.z.ts:{
    / if noone is connected then suicide - setup after first call
    .z.ts:{if[0=count[.z.W];show string[system"p"],": dying - no connections"; exit 0];};
    }

/ set suicide timer
system "t 1000";
show "testRunner available @ ",string[.z.h],":",string system "p";