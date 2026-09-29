# Repository architecture

Quantum Deep Calm AI keeps runtime source, deployable application assets, and non-runtime marketing material separate.

- `app/`: Android application source, resources, tests and build configuration.
- `.github/workflows/`: CI, security analysis, build and repository policy gates.
- `scripts/`: repository-maintenance automation only.
- Runtime assets required by the application belong under the appropriate `app/src/main/res/` resource directory.
- Large media, editable design sources, archives and marketing collateral do not belong in this repository. Publish them through the project's external CDN/content store instead.

## Repository policy

`Repo Guard` runs on every pull request and every push to `main`. It rejects Git LFS pointers, forbidden binary/design/archive formats, obsolete temporary directories, root-level marketing assets, tracked files larger than 5 MiB, and whitespace errors.

Run `scripts/clean-repo.sh` for a dry-run of ignored residue; add `--apply` only after reviewing the proposed removals.

## Build

Android validation is performed by the existing Android CI workflows using the pinned Gradle wrapper. Cloudflare Pages content, if maintained for this product, must be built/deployed from a dedicated web surface rather than mixing generated web output or marketing binaries into the Android source tree.
