<!--
    This is README is written in Markdown
    Please refer to here for an introduction to Markdown
    http://daringfireball.net/projects/markdown/
-->
RKPR
====

How to setup the RKPR Development Environment
---------------------------------------------

Clone a copy of the main RKPR repo

```bash
git clone https://github.com/jried31/RKPR.git
```

### Install Node.js

[https://github.com/joyent/node/wiki/Installing-Node.js-via-package-manager](https://github.com/joyent/node/wiki/Installing-Node.js-via-package-manager)

### Install Grunt Command Line Interface

[Grunt](http://gruntjs.com/getting-started) is a Javscript Task Runner that lets you automate minification, compilation, unit testing, linting (checking your code usage) , etc.

```
npm install -g grunt-cli
```
The job of the Grunt CLI is simple: run the version of Grunt which has been installed next to a Gruntfile

### Setup Node.js Environment 

Enter Node.js directory
```
cd Cloud
```

Option 1: Install dependencies for production
```
npm install --production
```

Option 2: Install dependencies for development 
```
npm install
```

### JavaScript Documentation

[jsdoc](http://usejsdoc.org/index.html) is used to document the JavaScript.
jsdoc is already included already installed as a dependency

To generate documentation run (Make sure you are in the ```Cloud``` directory)
```
npm run doc
```
The documentation is saved into ```Cloud/jsdoc```

Read the [documentation](http://jried31.github.io/RKPR/) 
It is saved under the `gh-pages` branch.

### JavaScript Testing
[mocha](http://visionmedia.github.io/mocha/) is a framework for running tests on Node.js

[expect.js](https://github.com/LearnBoost/expect.js) is a Behavior Driven Development(BDD) testing library.

Both **mocha** and **expect.js** are installed as development dependencies after running ```npm install```.

To run tests (Make sure you are in the ```Cloud``` directory)

```
npm run test
```
### Saving Dependecies

The dependencies for the Node.js application is managed via ```package.json``` in the ```Cloud/``` directory.
Please refer to **devDependencies** and **dependencies** for samples of dependencies that are saved.
There is no need to version control node packages because simply running```npm install``` will install packages specified in ```package.json```

#### Saving Production Dependencies
If you are installing a new dependency and need to save it as a production depenendency run:
```
npm i <package> --save
```
For more information please refer to [here](https://www.npmjs.org/doc/cli/npm-install.html);

#### Save Development Dependencies 
If you are installing a new dependency and need to save it as a development depenendency run:
```
npm i <package> --save-dev
```

### Using Grunt

Make sure you have ```grunt-cli``` installed before continuing. See above for installation instructions.
As mentioned, [Grunt](http://gruntjs.com/getting-started) is a Javscript Task Runner that lets you automate minification, compilation, unit testing, linting (checking your code usage) , etc.
The configuration for these tasks are specified in ```Gruntfile.js```

Some tasks that have been configured
```
grunt // executes the default tasks that have been configured (currently executes jsonlint and jshint)
```
```
grunt jsonlint // detect errors in JSON files
```
```
grunt jshint // detect errors and potential and problems in your JavaScript code
```

