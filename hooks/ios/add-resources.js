module.exports = function (context) {

  function logMe(what) {
    console.error(what);
  }

  logMe("START Running hook to copy any available google-services file to iOS");

  var fs = require('fs');

  var getValue = function(config, name) {
      var value = config.match(new RegExp('<' + name + '>(.*?)</' + name + '>', "i"));
      if(value && value[1]) {
          return value[1];
      } else {
          return null;
      }
  };

  function fileExists(path) {
    try  {
      return fs.statSync(path).isFile();
    } catch (e) {
      logMe("fileExists error: " + e);
      return false;
    }
  }

  function directoryExists(path) {
    try  {
      return fs.statSync(path).isDirectory();
    } catch (e) {
      logMe("directoryExists error: " + e);
      return false;
    }
  }

  function addResourceToXcodeProject(resourceName, isEntitlementFile) {
    var xcode = require('xcode'),
        path = require('path'),
        plist = require('plist'),
        util = require('util');

    var iosPlatform = path.join(context.opts.projectRoot, 'platforms/ios/');
    var iosFolder = fs.existsSync(iosPlatform) ? iosPlatform : context.opts.projectRoot;

    var data = fs.readdirSync(iosFolder);
    var projFolder;
    var projName;

    // Find the project folder by looking for *.xcodeproj
    if (data && data.length) {
      data.forEach(function (folder) {
        if (folder.match(/\.xcodeproj$/)) {
          projFolder = path.join(iosFolder, folder);
          projName = path.basename(folder, '.xcodeproj');
        }
      });
    }

    if (!projFolder || !projName) {
      throw new Error("Could not find an .xcodeproj folder in: " + iosFolder);
    }

    var projectPath = path.join(projFolder, 'project.pbxproj');

    var pbxProject;
    if (context.opts.cordova.project) {
      pbxProject = context.opts.cordova.project.parseProjectFile(context.opts.projectRoot).xcode;
    } else {
      pbxProject = xcode.project(projectPath);
      pbxProject.parseSync();
    }

    if (isEntitlementFile) {
      pbxProject.addResourceFile(projName + resourceName);
      var configGroups = pbxProject.hash.project.objects['XCBuildConfiguration'];
      for (var key in configGroups) {
        var config = configGroups[key];
        if (config.buildSettings !== undefined) {
          config.buildSettings.CODE_SIGN_ENTITLEMENTS = '"' + projName + '/Resources/' + projName + '.entitlements"';
        }
      }

      var projectPlistPath = path.join(iosFolder, projName, util.format('%s-Info.plist', projName));
      var projectPlistJson = plist.parse(fs.readFileSync(projectPlistPath, 'utf8'));
      var associatedDomainsApplink = projectPlistJson.AssociatedDomainsApplink;
      console.log("associatedDomainsApplink: " + associatedDomainsApplink);

      var entitlementsFile = path.join(iosFolder, projName, "Resources", projName + ".entitlements");
      console.log("entitlementsFile: " + entitlementsFile);
      var entitlementsFileXml = fs.readFileSync(entitlementsFile, 'utf8');
      var entitlementsFileObj = plist.parse(entitlementsFileXml);
      entitlementsFileObj["com.apple.developer.associated-domains"] = ["applinks:" + associatedDomainsApplink];
      entitlementsFileXml = plist.build(entitlementsFileObj);
      fs.writeFileSync(entitlementsFile, entitlementsFileXml, { encoding: 'utf8' });

    } else {
      pbxProject.addResourceFile(resourceName);
    }

    fs.writeFileSync(projectPath, pbxProject.writeSync());
  }

  if (directoryExists("platforms/ios")) {
    var paths = ["GoogleService-Info.plist", "platforms/ios/www/GoogleService-Info.plist"];

    for (var i = 0; i < paths.length; i++) {
      if (fileExists(paths[i])) {
        try {
          var contents = fs.readFileSync(paths[i]).toString();
          logMe("Found this file to write to iOS: " + paths[i]);
          var destFolder = "platforms/ios/" + projName + "/Resources";
          if (!fs.existsSync(destFolder)) {
            fs.mkdirSync(destFolder);
          }
          fs.writeFileSync(destFolder + "/GoogleService-Info.plist", contents);
          addResourceToXcodeProject("GoogleService-Info.plist");
        } catch (err2) {
          logMe(err2);
        }
        break;
      }
    }

    addResourceToXcodeProject(".entitlements", true);
  }
  logMe("END Running hook to copy any available google-services file to iOS");
};