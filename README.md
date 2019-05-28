# geo-scala

A core AST and utilities for GeoJSON ([RFC 7946][rfc-7946]) and more.

The project is divided in several submodules:
 - `core` contains the data model for geographical entities;
 - `circe` contains a set of [circe][circe] encoders and decoders for GeoJSON data model;
 - `polyline` contains utilities to convert GeoJSON line strings to and from [polylines][polyline].

## Quickstart

Add the following dependency in your sbt build configuration:

```scala
"com.free2move" %% "geo-scala" % "0.1.0"
```

## Code of Conduct

This project uses the Scala Code of Conduct - see [here](CODE_OF_CONDUCT.md).

## License

Copyright © 2019 GHM Mobile Development GmbH.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[rfc-7946]: https://tools.ietf.org/html/rfc7946
[circe]: https://circe.github.io/circe
[polyline]: https://developers.google.com/maps/documentation/utilities/polylineutility
