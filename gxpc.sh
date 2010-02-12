# This shell script compiles the gxps in the project.  If you add a new gxp source directory,
# add that as an argument to the compiler here as well.

java -cp war/WEB-INF/lib/gxp-0.2.4-beta.jar com.google.gxp.compiler.cli.Gxpc --dir genfiles --source src --output_language java src/com/google/livingstories/gxps/*.gxp
