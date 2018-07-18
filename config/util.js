if (!Object.assign) {
  Object.defineProperty(Object, 'assign', {
    enumerable: false,
    configurable: true,
    writable: true,
    value: function(target) {
      'use strict';
      if (target === undefined || target === null) {
        throw new TypeError('Cannot convert first argument to object');
      }

      var to = Object(target);
      for (var i = 1; i < arguments.length; i++) {
        var nextSource = arguments[i];
        if (nextSource === undefined || nextSource === null) {
          continue;
        }
        nextSource = Object(nextSource);

        var keysArray = Object.keys(nextSource);
        for (var nextIndex = 0, len = keysArray.length; nextIndex < len; nextIndex++) {
          var nextKey = keysArray[nextIndex];
          var desc = Object.getOwnPropertyDescriptor(nextSource, nextKey);
          if (desc !== undefined && desc.enumerable) {
            to[nextKey] = nextSource[nextKey];
          }
        }
      }
      return to;
    }
  });
}

function normalizeToVersion(str) {
    return str.replace(/\//gi, '-').replace(/[^a-zA-Z0-9.\-\+]/gi, '')
}

function brandByBranch(brand, branch) {
    var branch_normalized = normalizeToVersion(project.getProperty('git.branch'))

    switch(project.getProperty('git.branch')) {
        case 'master':
            break;
        case 'develop':
            brand += '_beta';
            break;
        default:
            brand += '_beta_' + branch_normalized;
    }

    return brand;
}