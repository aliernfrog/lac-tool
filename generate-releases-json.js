const { existsSync, readFileSync, writeFileSync } = require("fs");

const REPO = process.env.REPO ?? "aliernfrog/lac-tool";
const EPOCH_VERSION = process.env.EPOCH_VERSION ?? "v4.1.0";
const RELEASES_JSON = "./releases.json";

/**
 * Due to an oversight in updates screen, following versions had a broken update flow
 * which kept using the download URL and web URL of the already installed version.
 * This makes the user keep installing the same version and not the update itself.
 * 
 * To workaround this, simply override the `downloadUrl` and `htmlUrl` of
 * those releases with the latest version ones.
 */
const USE_LATEST_VERSION_LINKS_FOR = [ "v4.1.0" ];
let latestRelease;

const data = [];

async function main() {
  loadExistingData();
  await generateMissingReleases();
  console.log("Finished generating versions data");
  writeFileSync(RELEASES_JSON, JSON.stringify(
    data.sort((a,b) => b.versionCode - a.versionCode),
    null,
    2
  ));
  console.log(`Saved releases info to file: ${RELEASES_JSON}`);
}

function loadExistingData() {
  if (!existsSync(RELEASES_JSON)) return console.warn(`${RELEASES_JSON} does not exist, will generate all from epoch ${EPOCH_VERSION}`);
  try {
    const content = readFileSync(RELEASES_JSON).toString();
    const json = JSON.parse(content);
    if (!Array.isArray(json)) return console.warn(`Data in ${RELEASES_JSON} is not an array, skipping`);
    data.push(...json);
    console.log(`Loaded ${json.length} releases from ${RELEASES_JSON}`);
  } catch (e) {
    console.error(`Failed to load existing data from ${RELEASES_JSON}`, e);
  }
}

async function generateMissingReleases() {
  const releases = await (
    await fetch(`https://api.github.com/repos/${REPO}/releases`)
  ).json();
  latestRelease = releases[0];
  if (latestRelease) latestRelease.apk = latestRelease.assets.find(a => a.name.endsWith(".apk"));
  const startFrom = (data.length ? data[0].tag_name : null) ?? EPOCH_VERSION;
  const untilIndex = releases.findIndex(r => r.tag_name === startFrom);
  const missingReleases = releases.slice(0, untilIndex+1).filter(
    release => !data.some(d => d.versionName == release.tag_name)
  );
  console.log(`Found ${missingReleases.length} missing releases`);
  for (const release of missingReleases) {
    data.push(await generateReleaseInfo(release));
  }
  console.log(`Generated release info for ${missingReleases.length} missing releases`);
}

async function generateReleaseInfo(release) {
  const apkFile = release.assets.find(a => a.name.endsWith(".apk"));
  const { versionCode, minSdk } = await fetchExtrasFromGradleFile(release);
  const info = {
    versionName: release.name.toString(),
    versionCode,
    prerelease: release.prerelease,
    minSdk,
    body: fixBody(release.body),
    createdAt: Date.parse(release.created_at),
    htmlUrl: release.html_url,
    downloadUrl: apkFile.browser_download_url
  }
  if (USE_LATEST_VERSION_LINKS_FOR.includes(release.tag_name)) {
    info.htmlUrl = latestRelease.html_url;
    info.downloadUrl = latestRelease.apk.browser_download_url;
  }
  console.log(`Generated release info for ${release.tag_name}`);
  return info;
}

function fixBody(body) {
  return body
    .replaceAll(":boom:", "💥") // breaking changes
    .replaceAll(":sparkles:", "✨") // feat
    .replaceAll(":bug:", "🐛") // fix
    .replaceAll(":recycle:", "♻️") // refactor
    .replaceAll(":zap:", "⚡"); // perf
}

async function fetchExtrasFromGradleFile(release) {
  const tagName = release.tag_name;
  console.log(`Fetching gradle file extras for release: ${tagName}`);
  const gradleContent = await (
    await fetch(`https://raw.githubusercontent.com/${REPO}/${tagName}/app/build.gradle.kts`)
  ).text();
  const gradleLines = gradleContent.split("\n");
  const parseProp = (prop) => {
    const propDef = `${prop} =`;
    return gradleLines.find(l => l.includes(propDef))
      .replace(propDef, "").trim();
  }
  const extras = {
    versionCode: parseInt(parseProp("versionCode")),
    minSdk: parseInt(parseProp("minSdk"))
  }
  console.log(`Fetched extras for release: ${tagName}`);
  return extras;
}

main();