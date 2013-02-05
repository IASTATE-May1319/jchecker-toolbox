package toolbox.script

object Util extends App {

  import com.ensoftcorp.atlas.java.core.query.Q
  import com.ensoftcorp.atlas.java.core.query.Attr.Edge
  import com.ensoftcorp.atlas.java.core.query.Attr.Node
  import com.ensoftcorp.atlas.java.core.script.Common._
  import com.ensoftcorp.atlas.java.core.highlight._
  import com.ensoftcorp.atlas.java.interpreter.lib.Common._

  def saveDemo(methodName:String) = {
	// calculates the reverse call graph using the predefined methods above
    import com.ensoftcorp.atlas.java.core.highlight
    import java.awt.Color
    var e = universe.methods(methodName)
    var q = universe;
    var h = new Highlighter
    h.highlight(e, Color.GREEN)

    // PNG output file
    import org.eclipse.core.resources.ResourcesPlugin
    var projectName = "com.ensoftcorp.atlas.java.example.interpreter";
    var pngName = "out.png";
    var out = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getFile(pngName);
    var file = out.getRawLocation().toFile().getAbsoluteFile();

    var job = save(q, file, highlighter=h);
    
    // block until save is complete
    job.join();
  }
}