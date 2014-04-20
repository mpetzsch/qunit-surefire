
/ find all tests - must be in .test namespace and start test_
.qunit.getTests:{
    `$".test.",/:string f where (f:system["f .test"]) like "test_*"
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