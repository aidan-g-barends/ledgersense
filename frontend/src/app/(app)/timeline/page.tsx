import { PageHeader } from "@/components/ui/page-header";
import { TimelineView } from "@/components/timeline/timeline-view";

export default function TimelinePage() {
  return (
    <>
      <PageHeader title="Timeline" description="How your cash moved day by day. Red dots are days with flagged transactions." />
      <TimelineView />
    </>
  );
}