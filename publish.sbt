credentials += Credentials(
  "Free2Move Open Source",
  "gpg",
  "99A2E276448DC1DBA4F8D94EDFDF1B60ED0BFD83",
  "ignored"
)

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_USERNAME", ""),
  sys.env.getOrElse("SONATYPE_PASSWORD", "")
)
