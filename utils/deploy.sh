#!/bin/bash

get_current_version() {
  # Run maven command and get the first line that contains a valid version number
  version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>&1 | grep -m 1 -o '[0-9]\+\.[0-9]\+\.[0-9]\+\(-SNAPSHOT\)\?')

  # Check if we found a valid version
  if [[ -z "$version" ]]; then
    echo "Error: Could not extract valid version number" >&2
    exit 1
  fi

  echo "$version"
}

set_new_version() {
  local new_version=$1
  mvn versions:set -DnewVersion="$new_version"
  mvn versions:commit
}

commit_and_tag_version() {
  local version=$1
  git commit -am ":bookmark: New $2 version: $version :bookmark:"
  git tag -a "v$version" -m "New $2 version: $version"

  git push --follow-tags
}

version_patch() {
  current_version=$(get_current_version)
  # Validate version format
  if [[ ! $current_version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$ ]]; then
    echo "Error: Invalid version format: $current_version"
    exit 1
  fi

  new_version=$(echo "$current_version" | sed 's/-SNAPSHOT//' | awk -F. '{print $1"."$2"."$3}')
  set_new_version "$new_version"
  commit_and_tag_version "$new_version" "patch"
  snapshot_version
}

version_minor() {
  current_version=$(get_current_version)
  # Validate version format
  if [[ ! $current_version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$ ]]; then
    echo "Error: Invalid version format: $current_version"
    exit 1
  fi

  new_version=$(echo "$current_version" | sed 's/-SNAPSHOT//' | awk -F. '{print $1"."$2+1".0"}')
  set_new_version "$new_version"
  commit_and_tag_version "$new_version" "minor"
  snapshot_version
}

version_major() {
  current_version=$(get_current_version)
  # Validate version format
  if [[ ! $current_version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$ ]]; then
    echo "Error: Invalid version format: $current_version"
    exit 1
  fi

  new_version=$(echo "$current_version" | sed 's/-SNAPSHOT//' | awk -F. '{print $1+1".0.0"}')
  set_new_version "$new_version"
  commit_and_tag_version "$new_version" "major"
  snapshot_version
}

snapshot_version() {
  current_version=$(get_current_version)
  # Validate version format
  if [[ ! $current_version =~ ^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$ ]]; then
    echo "Error: Invalid version format: $current_version"
    exit 1
  fi

  # Extract major.minor.patch using a more robust method
  base_version=$(echo "$current_version" | sed 's/-SNAPSHOT//')
  major=$(echo "$base_version" | cut -d. -f1)
  minor=$(echo "$base_version" | cut -d. -f2)
  patch=$(echo "$base_version" | cut -d. -f3)

  # Increment patch
  patch=$((patch + 1))

  # Create new version
  new_version="${major}.${minor}.${patch}-SNAPSHOT"

  set_new_version "$new_version"

  git commit -am ":bookmark: New snapshot version: $new_version :bookmark:"
  git push
}

if [ -z "$1" ]; then
  echo "No action specified. Please choose an option:"
  echo "1) patch"
  echo "2) minor"
  echo "3) major"
  read -p "Enter your choice (1/2/3) : " choice

  case $choice in
    1)
      action="patch"
      ;;
    2)
      action="minor"
      ;;
    3)
      action="major"
      ;;
    *)
      echo "Invalid choice. Please run the script again."
      exit 1
      ;;
  esac
else
  action=$1
fi

case $action in
  patch)
    echo "Applying patch version..."
    version_patch
    ;;
  minor)
    echo "Applying minor version..."
    version_minor
    ;;
  major)
    echo "Applying major version..."
    version_major
    ;;
  *)
    echo "Usage: $0 {patch|minor|major}"
    exit 1
    ;;
esac